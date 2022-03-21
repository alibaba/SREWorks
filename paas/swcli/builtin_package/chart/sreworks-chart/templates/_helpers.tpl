

{{- define "swcli.endpoint" -}}
{{- if .Values.swcli.endpoint -}}
{{- .Values.swcli.endpoint -}}
{{- else -}}
{{- printf "http://%s-%s" .Release.Name "appmanager" -}}
{{- end -}}
{{- end -}}

{{- define "images.swcliBuiltinPackage" -}}
{{- if .Values.images.swcliBuiltinPackage -}}
{{- .Values.images.swcliBuiltinPackage -}}
{{- else -}}
{{- printf "%s/%s:%s" .Values.global.images.registry "swcli-builtin-package" .Values.global.images.tag -}}
{{- end -}}
{{- end -}}


{{- define "domain.base.ingress" -}}
{{- $domain := (split "://" .Values.appmanager.home.url)._1  -}}
{{- (splitn "." 2 $domain)._1 -}}
{{- end -}}

{{- define "domain.base.ingress.networkProtocol" -}}
{{- (split "://" .Values.appmanager.home.url)._0 | quote -}}
{{- end -}}

{{- define "dataops.namespace" -}}
{{- printf "%s-%s" .Release.Namespace "dataops" -}}
{{- end -}}

{{- define "aiops.namespace" -}}
{{- printf "%s-%s" .Release.Namespace "aiops" -}}
{{- end -}}





