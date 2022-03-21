curl -X POST '{{ url }}' \
-H 'x-auth-app: {{ app }}' \
-H 'x-auth-key: {{ key }}' \
-H 'x-auth-user: {{ user }}' \
-H 'x-auth-passwd: {{ passwd }}' \
-H 'accept: */*' \
-H 'Content-Type: application/json' \
-d '{{ input }}'