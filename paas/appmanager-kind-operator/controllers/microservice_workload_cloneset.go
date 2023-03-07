package controllers

import (
	appmanagerabmiov1 "appmanager-operator/api/v1"
	"appmanager-operator/helper"
	"context"
	"errors"
	"fmt"
	kruiseapps "github.com/openkruise/kruise-api/apps/v1alpha1"
	errors2 "github.com/pkg/errors"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

// +kubebuilder:rbac:groups=apps.kruise.io,resources=clonesets,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=apps.kruise.io,resources=clonesets/status,verbs=get

// constructForCloneSet 用于通过 Microservice 对象构建 CloneSet 对象
func (r *MicroserviceReconciler) constructForCloneSet(raw *appmanagerabmiov1.Microservice) (*kruiseapps.CloneSet, error) {
	cloneSet := &kruiseapps.CloneSet{
		ObjectMeta: metav1.ObjectMeta{
			Name:        raw.Name,
			Namespace:   raw.Namespace,
			Labels:      raw.ObjectMeta.Labels,
			Annotations: raw.ObjectMeta.Annotations,
		},
		Spec: *raw.Spec.CloneSet,
	}

	// set CloneSet metadata
	cloneSet.Spec.Template.ObjectMeta = metav1.ObjectMeta{
		Name:        raw.Name,
		Namespace:   raw.Namespace,
		Labels:      raw.ObjectMeta.Labels,
		Annotations: raw.ObjectMeta.Annotations,
	}

	// set owner reference
	if err := ctrl.SetControllerReference(raw, cloneSet, r.Scheme); err != nil {
		return nil, err
	}
	return cloneSet, nil
}

func (r *MicroserviceReconciler) ReconcileMicroserviceCloneSet(ctx context.Context, req ctrl.Request,
	microservice *appmanagerabmiov1.Microservice, targetRevision string) (bool, error) {

	log := r.Log.WithValues("microservice", req.NamespacedName)

	// 前置检查
	if microservice.Spec.CloneSet == nil {
		log.Error(errors.New("CloneSet spec is not set, abort"), "cannot reconcile microservice cloneset")
		return false, nil
	}

	// 当进行 kind 变换的时候，删除所有非当前类型的其他资源
	if err := helper.RemoveUselessKindResource(r.Client, ctx, &req, helper.ClonesetOwnerKey); err != nil {
		log.Error(err, "remove useless kind resource failed")
		return false, err
	}

	// 获取当前 CR 下所有附属的 CloneSet 列表
	var instance kruiseapps.CloneSet
	err := r.GetClient().Get(ctx, req.NamespacedName, &instance)

	// 当不存在 CloneSet 的时候直接新建并返回，否则进行更新
	if err != nil {
		if apierrors.IsNotFound(err) {
			// 更新状态
			microservice.Status.Condition = appmanagerabmiov1.MicroserviceProgressing
			if err := r.Status().Update(ctx, microservice); err != nil {
				return false, err
			}
			log.V(1).Info(fmt.Sprintf("update microservice %s status to %s",
				microservice.Name, appmanagerabmiov1.MicroserviceProgressing))

			// 创建 CloneSet
			cloneset, err := r.constructForCloneSet(microservice)
			if err != nil {
				log.Error(err, "unable to construct CloneSet in kubernetes context")
				return false, err
			}
			if err := r.Create(ctx, cloneset); err != nil {
				log.Error(err, "unable to create CloneSet in kubernetes context", "CloneSet", cloneset)
				return false, err
			}
			log.V(1).Info("created CloneSet for appmanager microservice", "CloneSet", cloneset)
			return true, nil
		} else {
			errorMessage := fmt.Sprintf("cannot get cloneset instance, location=%+v", req.NamespacedName)
			log.Error(err, errorMessage)
			return false, errors2.Wrap(err, errorMessage)
		}
	}

	// 获取 revision 对象，并计算 hash 是否一致
	currentRevision, err := helper.GetRevision(&instance.ObjectMeta)
	if err != nil {
		log.Error(err, fmt.Sprintf("unable to get current hash in CloneSet %+v", instance.ObjectMeta))
		return false, err
	}

	// 更新状态
	changed := false
	finalCondition := appmanagerabmiov1.MicroserviceUnknown
	if currentRevision != targetRevision ||
		instance.Status.CurrentRevision != instance.Status.UpdateRevision ||
		instance.Status.ReadyReplicas < *instance.Spec.Replicas {
		finalCondition = appmanagerabmiov1.MicroserviceProgressing
	} else if instance.Status.ReadyReplicas == *instance.Spec.Replicas {
		finalCondition = appmanagerabmiov1.MicroserviceAvailable
	}
	if microservice.Status.Condition != finalCondition {
		changed = true
		microservice.Status.Condition = finalCondition
		if err := r.Status().Update(ctx, microservice); err != nil {
			return false, err
		}
		log.V(1).Info(fmt.Sprintf("update microservice %s status to %s", microservice.Name, finalCondition))
	}

	// 检查 revision
	if currentRevision == targetRevision {
		log.Info("current microservice revision is equal to CloneSet revision, skip")
		return changed, nil
	}

	// 进行 Spec 替换
	*instance.Spec.Replicas = *microservice.Spec.CloneSet.Replicas
	instance.Spec.Template = microservice.Spec.CloneSet.Template
	labels := microservice.ObjectMeta.Labels
	if fmt.Sprint(labels) != fmt.Sprint(instance.Spec.Selector.MatchLabels) {
		log.V(1).Info("found unmatched labels between microservice and advancedstatefulsets, use selector instead",
			"MatchLabels", instance.Spec.Selector.MatchLabels, "MicroserviceLabels", labels)
		labels = instance.Spec.Selector.MatchLabels
	}
	instance.Spec.Template.ObjectMeta = metav1.ObjectMeta{
		Name:        microservice.Name,
		Namespace:   microservice.Namespace,
		Labels:      labels,
		Annotations: microservice.ObjectMeta.Annotations,
	}
	instance.Spec.Lifecycle = microservice.Spec.CloneSet.Lifecycle
	instance.Spec.ScaleStrategy = microservice.Spec.CloneSet.ScaleStrategy
	instance.Spec.UpdateStrategy = microservice.Spec.CloneSet.UpdateStrategy
	instance.Spec.MinReadySeconds = microservice.Spec.CloneSet.MinReadySeconds
	if microservice.Spec.CloneSet.RevisionHistoryLimit != nil {
		*instance.Spec.RevisionHistoryLimit = *microservice.Spec.CloneSet.RevisionHistoryLimit
	}
	instance.Spec.VolumeClaimTemplates = microservice.Spec.CloneSet.VolumeClaimTemplates
	_ = helper.SetRevision(&instance.ObjectMeta, targetRevision)
	if err := r.GetClient().Update(ctx, &instance); err != nil {
		log.Error(err, "unable to update clonesets, prepare to delete", "CloneSet", instance)
		if err := r.Delete(ctx, &instance, client.PropagationPolicy(metav1.DeletePropagationForeground)); client.IgnoreNotFound(err) != nil {
			log.Error(err, "unable to delete conflict clonesets", "CloneSet", instance)
			return false, err
		}
		log.V(1).Info("deleted conflict clonesets", "CloneSet", instance)
		return false, err
	}
	log.V(1).Info(fmt.Sprintf("update CloneSet %s spec to %+v", microservice.Name, instance.Spec))
	return true, nil
}
