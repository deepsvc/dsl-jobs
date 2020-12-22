def jenkinsfile = "Jenkinsfile"
pipelineJob("wade-deployment") {
  parameters {
  stringParam("REGION", "us-east-1", description="")
  stringParam("BRANCH", "", description="")
  stringParam("APPLICATION_NAME", "", description="")
  choiceParam("ENV", ["dev", "test", "uat"], description="")
  booleanParam("S3_BUCKETS", false, "")
  booleanParam("LAMBDA_LAYERS", false, "")
  booleanParam("DDB_TABLE", false, "")
  booleanParam("PINPOINT", false, "")
  booleanParam("USER_POOL", false, "")
  booleanParam("LAMBDA_STACK", false, "")
  booleanParam("API", true, "")
  booleanParam("WAF", false, "")
  booleanParam("CLOUDFRONT", false, "")
  
}
    logRotator(numToKeep = 100)
    definition {
        cps {
            sandbox()
            script(readFileFromWorkspace(jenkinsfile))
        }
    }
}
