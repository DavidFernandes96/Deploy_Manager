Vagrant.configure("2") do |config|
config.vm.define "ubuntu-vm" do |vm1|
vm1.vm.hostname = "ubuntu-vm"
vm1.vm.box = "ubuntu-VAGRANTSLASH-trusty64"
vm1.vm.network "private_network", ip: "192.168.33.10"
vm1.vm.provision "shell", inline: <<-SHELL
echo "THIS IS SUPPOSED TO BE AN UPGRADE!!"
SHELL
end
config.vm.define "lubuntu" do |vm2|
vm2.vm.hostname = "lubuntu"
vm2.vm.box = "lubuntu"
vm2.vm.network "private_network", ip: "192.168.33.11"
vm2.vm.provision "shell", inline: <<-SHELL
echo "THIS IS SUPPOSED TO BE AN UPGRADE!!"
SHELL
end
end