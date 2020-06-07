Vagrant.configure("2") do |config|
config.vm.define "Ubuntu" do |vm1|
vm1.vm.hostname = "Ubuntu"
vm1.vm.box = "ubuntu-VAGRANTSLASH-trusty64"
vm1.vm.network "private_network", ip: "192.168.33.17"
end
config.vm.define "Lubuntu" do |vm2|
vm2.vm.hostname = "Lubuntu"
vm2.vm.box = "lubuntu"
vm2.vm.network "private_network", ip: "192.168.33.11"
end
config.vm.define "CentOS" do |vm3|
vm3.vm.hostname = "CentOS"
vm3.vm.box = "centOS"
vm3.vm.network "private_network", ip: "192.168.33.15"
vm3.vm.provision "shell", inline: <<-SHELL
echo "another fucking update"
SHELL
end
end