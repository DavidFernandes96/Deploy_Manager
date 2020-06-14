Vagrant.configure("2") do |config|
config.vm.define "Ubuntu" do |vm1|
vm1.vm.hostname = "Ubuntu"
vm1.vm.box = "ubuntu-VAGRANTSLASH-trusty64"
vm1.vm.network "private_network", ip: "192.168.33.10"
end
config.vm.define "CentOS" do |vm2|
vm2.vm.hostname = "CentOS"
vm2.vm.box = "centOS"
vm2.vm.network "private_network", ip: "192.168.33.25"
vm2.vm.provision "shell", inline: <<-SHELL
yum -y update
SHELL
end
config.vm.define "CentOSv2" do |vm3|
vm3.vm.hostname = "CentOSv2"
vm3.vm.box = "centOS"
vm3.vm.network "private_network", ip: "192.168.33.26"
end
config.vm.define "Ubuntu2" do |vm4|
vm4.vm.hostname = "Ubuntu2"
vm4.vm.box = "ubuntu-VAGRANTSLASH-trusty64"
vm4.vm.network "private_network", ip: "192.168.33.17"
vm4.vm.provision "shell", inline: <<-SHELL
apt-get update
apt-get -y upgrade
SHELL
end
config.vm.define "Lubuntu" do |vm5|
vm5.vm.hostname = "Lubuntu"
vm5.vm.box = "lubuntu"
vm5.vm.network "private_network", ip: "192.168.33.30"
end
end