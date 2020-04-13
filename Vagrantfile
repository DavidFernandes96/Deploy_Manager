# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  
  config.vm.define "ubuntu-vm" do |vm1|
    vm1.vm.hostname = "ubuntu-vm"
    vm1.vm.box = "ubuntu/trusty64"
    vm1.vm.network "private_network", ip: "192.168.33.10"
    
    vm1.vm.provider "virtualbox" do |vb|
      vb.name = "ubuntu-vm"
      vb.gui = false
      vb.memory = "1024"
    end

    vm1.vm.provision "shell", run: "always", inline: <<-SHELL
     echo "Hello from the Ubuntu VM"
    SHELL
  end

  config.vm.define "alpine-vm" do |vm2|
    #vm2.vm.hostname = "alpine-vm"  #vagrant nao suporta mudanca de hostname (base boxes??)
    vm2.vm.box = "Vagrant-Alpine"
    vm2.ssh.shell = "ash"
    #vm2.vm.network "private_network", ip: "192.168.33.11" #vagrant nao suporta mudanca de IP
    
    vm2.vm.provider "virtualbox" do |vb|
      vb.name = "alpine-vm"
      vb.gui = false
      vb.memory = "1024"
    end

    vm2.vm.provision "shell", run: "always", inline: <<-SHELL
     echo "Hello from the Alpine VM"
    SHELL
  end
  
end
