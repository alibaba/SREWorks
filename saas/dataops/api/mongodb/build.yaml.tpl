componentType: HELM
componentName: mongodb
options:
  repoPath: saas/dataops/api/mongodb/mongodb-chart
  branch: ${SOURCE_BRANCH}
  repo: ${SOURCE_REPO}
  ciAccount: "${SOURCE_CI_ACCOUNT}"
  ciToken: "${SOURCE_CI_TOKEN}"

