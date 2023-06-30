#!/bin/bash

# Array of input file paths
input_files_tmp=$(cat filenames.txt | tr "\n" " ")
input_files=()
read -ra input_files <<< "$input_files_tmp"
# Output directory and text file to store new file names
output_directory="./Splits"
output_file="splits.txt"

# Clear the output file if it already exists
> "$output_file"

# Loop over the input files
for file in "${input_files[@]}"; do
  # Get the file name and extension
  filename=$(basename "$file")
  extension="${filename##*.}"
  filename="${filename%.*}"

  # Split the file into two parts
  split -n 2 "$file" "$output_directory/$filename"
done

ls -d $output_directory/* | tr " " "\n" > $output_file