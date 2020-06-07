Vagrant.configure("2") do |config|
config.vm.define "Ubuntu" do |vm1|
vm1.vm.hostname = "Ubuntu"
vm1.vm.box = "ubuntu-VAGRANTSLASH-trusty64"
vm1.vm.network "private_network", ip: "192.168.33.17"
vm1.vm.provision "shell", inline: <<-SHELL
apt-get update
apt-get -y upgrade
SHELL
end
config.vm.define "CentOS" do |vm2|
vm2.vm.hostname = "CentOS"
vm2.vm.box = "centOS"
vm2.vm.network "private_network", ip: "192.168.33.20"
vm2.vm.provision "shell", inline: <<-SHELL
echo "another fucking update"
SHELL
end
config.vm.define "Lubuntu" do |vm3|
vm3.vm.hostname = "Lubuntu"
vm3.vm.box = "lubuntu"
vm3.vm.network "private_network", ip: "192.168.33.14"
end
end