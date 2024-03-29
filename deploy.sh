#!/bin/bash
# A simple variable example
login="mabou-22"
remoteFolder="/tmp/$login/"
fileName="SimpleServerProgram"
fileExtension=".java"

machines=$(cat machines.txt | tr "\n" " ")
computers=()
read -ra computers <<< "$machines"

# computers=("tp-1a201-04.enst.fr" "tp-1a201-14.enst.fr" "tp-1a201-24.enst.fr" "tp-1a201-03.enst.fr" "tp-1a201-05.enst.fr" "tp-1a201-06.enst.fr" "tp-1a201-08.enst.fr" "tp-1a201-09.enst.fr" "tp-1a201-10.enst.fr" "tp-1a201-11.enst.fr" "tp-1a201-12.enst.fr" "tp-1a201-13.enst.fr")
# computers=("tp-3a101-01.enst.fr" "tp-3a101-10.enst.fr" "tp-3a107-05.enst.fr" "tp-3a107-13.enst.fr" "tp-3a107-14.enst.fr") # "tp-t309-00.enst.fr" "tp-t309-01.enst.fr" "tp-t309-02.enst.fr" "tp-t309-03.enst.fr")
#computers=("tp-1a226-01")
for c in ${computers[@]}; do
  command0=("ssh" "$login@$c" "lsof -ti | xargs kill -9")
  command1=("ssh" "$login@$c" "rm -rf $remoteFolder;mkdir $remoteFolder")
  command2=("scp" "$fileName$fileExtension" "$login@$c:$remoteFolder$fileName$fileExtension")
  command3=("ssh" "$login@$c" "cd $remoteFolder;javac $fileName$fileExtension;java -Xmx12g $fileName")
  echo ${command0[*]}
  "${command0[@]}"
  echo ${command1[*]}
  "${command1[@]}"
  echo ${command2[*]}
  "${command2[@]}" 
  echo ${command3[*]}
  "${command3[@]}" &
done
