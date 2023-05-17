#!/bin/bash
# A simple variable example
login="mabou-22"
remoteFolder="/tmp/$login/"
fileName="SimpleServerProgram"
fileExtension=".java"
computers=("tp-1a201-10.enst.fr" "tp-1a201-11.enst.fr" "tp-1a201-12.enst.fr")
#computers=("tp-1a226-01")
for c in ${computers[@]}; do
  command0=("ssh" "$login@$c" "lsof -ti | xargs kill -9")
  command1=("ssh" "$login@$c" "rm -rf $remoteFolder;mkdir $remoteFolder")
  command2=("scp" "$fileName$fileExtension" "$login@$c:$remoteFolder$fileName$fileExtension")
  command3=("ssh" "$login@$c" "cd $remoteFolder;javac $fileName$fileExtension;java $fileName")
  echo ${command0[*]}
  "${command0[@]}"
  echo ${command1[*]}
  "${command1[@]}"
  echo ${command2[*]}
  "${command2[@]}"
  echo ${command3[*]}
  "${command3[@]}" &
done