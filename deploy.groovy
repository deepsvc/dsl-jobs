/* groovylint-disable LineLength */
def jenkinsfile = 'Jenkinsfile'
pipelineJob('ansible-job') {
    parameters {
        stringParam('Docker_Image_Name', 'nginx-app', description = 'Name will be used for Docker Image Deployed by Terraform')
        stringParam('TF_Backend_Config_S3_Bucket_Name', 'test-bucket-terraform-project', description = 'S3 Bucket Name where TF State File will be Stored')
        stringParam('TF_Backend_Config_Key', 'network/terraform.tfstate', description = 'Path in the S3 Bucket where TF State will be Stored')
        stringParam('TF_Backend_Config_Region', 'ap-south-1', description = 'Region of the S3 Bucket')
        choiceParam('Terraform_Action', ['Init', 'Plan', 'Deploy_Planned', 'Deploy_Directly', 'Destroy'], description = 'Terraform Actions you want to perform')
    }
    logRotator(numToKeep = 100)
    definition {
        cps {
            sandbox()
            script(readFileFromWorkspace(jenkinsfile))
        }
    }
}
