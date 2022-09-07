componentType: HELM
componentName: prometheus
options:
  repoPath: saas/dataops/api/prometheus/prometheus-chart
  branch: ${SOURCE_BRANCH}
  repo: ${SOURCE_REPO}
  ciAccount: "${SOURCE_CI_ACCOUNT}"
  ciToken: "${SOURCE_CI_TOKEN}"

