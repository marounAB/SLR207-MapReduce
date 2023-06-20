#!/bin/bash

[[ -e index.html ]] && rm index.html

wget tp.telecom-paris.fr
cat index.html | grep "</tr>" | sed 's/<\/tr>/\n/g' | grep OK | grep -o tp-[1-9][a-z][0-9]*-[0-9]* | tail -n +2 | head -n 12 | awk '{print $1".enst.fr"}' > machines.txt

tmp=$(cat machines.txt | tr "\n" " ")

machines=()

read -ra machines <<< "$tmp"

for machine in "${machines[@]}"; do
  ssh -o StrictHostKeyChecking=no mabou-22@$machine "echo hello $machine"
done

