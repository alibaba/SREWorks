

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


{{- define "nodePort" -}}
{{- if eq .Values.global.accessMode "ingress" -}}
"80"
{{- else -}}
{{- (split ":" .Values.appmanager.home.url)._2 | quote -}}
{{- end -}}
{{- end -}}

{{- define "dataops.namespace" -}}
{{- printf "%s-%s" .Release.Namespace "dataops" -}}
{{- end -}}

{{- define "aiops.namespace" -}}
{{- printf "%s-%s" .Release.Namespace "aiops" -}}
{{- end -}}

{{- define "minio.endpoint" -}}
{{- if .Values.server.package.endpoint -}}
{{- .Values.server.package.endpoint | quote -}}
{{- else -}}
{{ .Release.Name }}-minio.{{ .Release.Namespace }}
{{- end -}}
{{- end -}}


