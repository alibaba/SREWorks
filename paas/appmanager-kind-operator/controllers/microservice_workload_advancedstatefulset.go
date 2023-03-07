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

// +kubebuilder:rbac:groups=apps.kruise.io,resources=statefulsets,verbs=get;list;watch;create;update;patch;delete
// +kubebuilder:rbac:groups=apps.kruise.io,resources=statefulsets/status,verbs=get

// constructForAdvancedStatefulSet 用于通过 Microservice 对象构建 AdvancedStatefulSet 对象
func (r *MicroserviceReconciler) constructForAdvancedStatefulSet(raw *appmanagerabmiov1.Microservice) (*kruiseapps.StatefulSet, error) {
	advancedStatefulSet := &kruiseapps.StatefulSet{
		ObjectMeta: metav1.ObjectMeta{
			Name:        raw.Name,
			Namespace:   raw.Namespace,
			Labels:      raw.ObjectMeta.Labels,
			Annotations: raw.ObjectMeta.Annotations,
		},
		Spec: *raw.Spec.AdvancedStatefulSet,
	}

	// set AdvancedStatefulSet metadata
	advancedStatefulSet.Spec.Template.ObjectMeta = metav1.ObjectMeta{
		Name:        raw.Name,
		Namespace:   raw.Namespace,
		Labels:      raw.ObjectMeta.Labels,
		Annotations: raw.ObjectMeta.Annotations,
	}

	// set owner reference
	if err := ctrl.SetControllerReference(raw, advancedStatefulSet, r.Scheme); err != nil {
		return nil, err
	}
	return advancedStatefulSet, nil
}

func (r *MicroserviceReconciler) ReconcileMicroserviceAdvancedStatefulSet(
	ctx context.Context, req ctrl.Request, microservice *appmanagerabmiov1.Microservice,
	targetRevision string) (bool, error) {

	log := r.Log.WithValues("microservice", req.NamespacedName)

	// 前置检查
	if microservice.Spec.AdvancedStatefulSet == nil {
		log.Error(errors.New("AdvancedStatefulSet spec is not set, abort"), "cannot reconcile microservice advancedStatefulSet")
		return false, nil
	}

	// 当进行 kind 变换的时候，删除所有非当前类型的其他资源
	if err := helper.RemoveUselessKindResource(r.Client, ctx, &req, helper.AdvancedstatefulsetOwnerKey); err != nil {
		log.Error(err, "remove useless kind resource failed")
		return false, err
	}

	// 获取当前对象
	var instance kruiseapps.StatefulSet
	err := r.GetClient().Get(ctx, req.NamespacedName, &instance)

	// 当不存在 AdvancedStatefulSet 的时候直接新建并返回，否则进行更新
	if err != nil {
		if apierrors.IsNotFound(err) {
			// 更新状态
			microservice.Status.Condition = appmanagerabmiov1.MicroserviceProgressing
			if err := r.Status().Update(ctx, microservice); err != nil {
				return false, err
			}
			log.V(1).Info(fmt.Sprintf("update microservice %s status to %s",
				microservice.Name, appmanagerabmiov1.MicroserviceProgressing))

			// 创建 AdvancedStatefulSet
			advancedStatefulSet, err := r.constructForAdvancedStatefulSet(microservice)
			if err != nil {
				log.Error(err, "unable to construct AdvancedStatefulSet in kubernetes context")
				return false, err
			}
			if err := r.Create(ctx, advancedStatefulSet); err != nil {
				log.Error(err, "unable to create AdvancedStatefulSet in kubernetes context", "AdvancedStatefulSet", advancedStatefulSet)
				return false, err
			}
			log.V(1).Info("created AdvancedStatefulSet for appmanager microservice", "AdvancedStatefulSet", advancedStatefulSet)
			return true, nil
		} else {
			errorMessage := fmt.Sprintf("cannot get advanced statefulset instance, location=%+v", req.NamespacedName)
			log.Error(err, errorMessage)
			return false, errors2.Wrap(err, errorMessage)
		}
	}

	// 获取 revision 对象，并计算 hash 是否一致
	currentRevision, err := helper.GetRevision(&instance.ObjectMeta)
	if err != nil {
		log.Error(err, fmt.Sprintf("unable to get current hash in AdvancedStatefulSet %+v", instance.ObjectMeta))
		return false, err
	}

	// 更新状态
	changed := false
	finalCondition := appmanagerabmiov1.MicroserviceUnknown
	if currentRevision != targetRevision ||
		len(instance.Status.CurrentRevision) == 0 ||
		len(instance.Status.UpdateRevision) == 0 ||
		instance.Status.CurrentRevision != instance.Status.UpdateRevision ||
		instance.Status.ReadyReplicas != instance.Status.Replicas ||
		instance.Status.ReadyReplicas != *instance.Spec.Replicas {
		finalCondition = appmanagerabmiov1.MicroserviceProgressing
	} else {
		finalCondition = appmanagerabmiov1.MicroserviceAvailable
	}
	if microservice.Status.Condition != finalCondition {
		changed = true
		microservice.Status.Condition = finalCondition
		if err := r.Status().Update(ctx, microservice); err != nil {
			return false, err
		}
		log.V(1).Info(fmt.Sprintf("update microservice %s status to %s", microservice.Name, finalCondition))
	} else {
		log.V(1).Info(fmt.Sprintf("no need to update microservice %s status", microservice.Name))
	}

	// 检查 revision
	if currentRevision == targetRevision {
		log.Info("current microservice revision is equal to AdvancedStatefulSet revision, skip")
		return changed, nil
	}

	// 进行 Spec 替换
	*instance.Spec.Replicas = *microservice.Spec.AdvancedStatefulSet.Replicas
	instance.Spec.Template = microservice.Spec.AdvancedStatefulSet.Template
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
	instance.Spec.UpdateStrategy = microservice.Spec.AdvancedStatefulSet.UpdateStrategy
	instance.Spec.VolumeClaimTemplates = microservice.Spec.AdvancedStatefulSet.VolumeClaimTemplates
	_ = helper.SetRevision(&instance.ObjectMeta, targetRevision)
	if err := r.GetClient().Update(ctx, &instance); err != nil {
		log.Error(err, "unable to update advancedstatefulsets, prepare to delete", "AdvancedStatefulSet", instance)
		if err := r.Delete(ctx, &instance, client.PropagationPolicy(metav1.DeletePropagationForeground)); client.IgnoreNotFound(err) != nil {
			log.Error(err, "unable to delete conflict advancedStatefulSet", "AdvancedStatefulSet", instance)
			return false, err
		}
		log.V(1).Info("deleted conflict advancedStatefulSet", "AdvancedStatefulSet", instance)
		return false, err
	}
	log.V(1).Info(fmt.Sprintf("update AdvancedStatefulSet %s spec to %+v", microservice.Name, instance.Spec))
	return true, nil
}
