componentType: HELM
componentName: arena
options:
  repoPath: saas/dataops/api/arena/arena-chart
  branch: ${SOURCE_BRANCH}
  repo: ${SOURCE_REPO}
  ciAccount: "${SOURCE_CI_ACCOUNT}"
  ciToken: "${SOURCE_CI_TOKEN}"

