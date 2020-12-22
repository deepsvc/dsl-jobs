node {
        stage('Clean Workspace') {
            if ("${Terraform_Action}" == 'Init') {
            cleanWs()
            }
        }

        stage('Checkout Repository') {
            if ("${Terraform_Action}" == 'Init') {
            sh 'git clone https://github.com/deepsvc/ansible-terraform-assignment-code.git'
            }
        }

        stage ('Write Config File for TF Backend') {
            writeFile file: 'ansible-terraform-assignment-code/config.tf', text: """
bucket = "${TF_Backend_Config_S3_Bucket_Name}"
key    = "${TF_Backend_Config_Key}"
region = "${TF_Backend_Config_Region}"

            """
        }

        stage ('Write Var File for Ansible') {
            writeFile file: 'ansible-terraform-assignment-code/vars/main.yml', text: """
---
doc_name: "${Docker_Image_Name}"
backend_config: "${workspace}/ansible-terraform-assignment-code/config.tf"
project_path: "${workspace}/ansible-terraform-assignment-code/terraform-docker-demo"
            """
        }

        stage('Run Terraform Init') {
            if ("${Terraform_Action}" == 'Init') {
            sh '''
                cd ansible-terraform-assignment-code
                ansible-playbook playbook.yml --skip-tags plan,absent,deploy_planned,deploy
                '''
            }
        }

        stage('Run Terraform Plan') {
            if ("${Terraform_Action}" == 'Plan') {
            sh '''
                cd ansible-terraform-assignment-code
                ansible-playbook playbook.yml --skip-tags absent,deploy_planned,deploy
                '''
            }
        }

        stage('Deploy Terraform Planned Template') {
            if ("${Terraform_Action}" == 'Deploy_Planned') {
            sh '''
                cd ansible-terraform-assignment-code
                ansible-playbook playbook.yml --skip-tags plan,absent,init,deploy
                '''
            }
        }

        stage('Deploy Terraform Planned Template') {
            if ("${Terraform_Action}" == 'Deploy_Directly') {
            sh '''
                cd ansible-terraform-assignment-code
                ansible-playbook playbook.yml --skip-tags plan,absent,init,deploy_planned
                '''
            }
        }

        stage('Deploy Terraform Planned Template') {
            if ("${Terraform_Action}" == 'Destroy') {
            sh '''
                cd ansible-terraform-assignment-code
                ansible-playbook playbook.yml --skip-tags plan,deploy_planned,init,deploy
                '''
            }
        }
}
