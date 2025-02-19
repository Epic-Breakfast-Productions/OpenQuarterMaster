# build-nginx.pkr.hcl

build {
   sources = [
     "source.amazon-ebs.oqm-ami"
   ]

  
   provisioner "file" {
    source = "./get-metadata.sh"
    destination = "/tmp/get-metadata.sh"
   }


   provisioner "shell" {
     inline = [
       "echo Updating Packages",
       "sudo apt-get update",
     ]
   }

   provisioner "shell" {
     inline = ["sudo wget -q -O - https://deployment.openquartermaster.com/repos/main/deb/setup-repo.sh > /tmp/setup_repo.sh",
               "sudo chmod +x /tmp/setup_repo.sh",
               "sudo /tmp/setup_repo.sh --auto",
               #"sudo apt-get install oqm-plugin-cloud-context"
               #"sudo oqm-config -s plugins.cloud-context.provider aws ''"
               ]

   }
}


