Vagrant.configure("2") do |config|
config.vm.define "Ubuntu" do |vm1|
vm1.vm.hostname = "Ubuntu"
vm1.vm.box = "ubuntu-VAGRANTSLASH-trusty64"
vm1.vm.network "private_network", ip: "192.168.33.10"
vm1.vm.provision "shell", inline: <<-SHELL
apt update
apt upgrade
SHELL
end
config.vm.define "Lubuntu" do |vm2|
vm2.vm.hostname = "Lubuntu"
vm2.vm.box = "lubuntu"
vm2.vm.network "private_network", ip: "192.168.33.20"
vm2.vm.provision "shell", inline: <<-SHELL
echo "helllooooo"
SHELL
end
config.vm.define "CentOS" do |vm3|
vm3.vm.hostname = "CentOS"
vm3.vm.box = "centOS"
vm3.vm.network "private_network", ip: "192.168.33.12"
vm3.vm.provision "shell", inline: <<-SHELL
yum update
SHELL
end
end