#!/bin/bash

login="mabou-22"

[[ -e index.html ]] && rm index.html

wget tp.telecom-paris.fr
ok_machines=$(cat index.html | grep "</tr>" | sed 's/<\/tr>/\n/g' | grep OK | grep -o tp-[1-9][a-z][0-9]*-[0-9]*  | awk '{print $1".enst.fr"}')

tmp=$(echo $ok_machines | tr "\n" " ")

machines=()

read -ra machines <<< "$tmp"

shuffled_machines=($(shuf -e "${machines[@]}"))

working_count=0

> machines.txt

for machine in "${shuffled_machines[@]}"; do
  ssh -q -o StrictHostKeyChecking=no -o BatchMode=yes -o ConnectTimeout=3 $login@$machine "exit"
  [[ $? -eq 0 ]] && echo "$machine" >> machines.txt && ((working_count++))
  [[ $working_count -eq $1 ]] && break
done
