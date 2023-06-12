#!/bin/bash

# Path to the file containing the list of gzipped files
file_list="wet.paths"

# Maximum number of iterations
max_iterations=3000

# Counter for iterations
iteration=0

# Loop through each line in the file
while IFS= read -r line; do
    # Increment the iteration counter
    ((iteration++))

    echo "Iteration: $iteration"

    # Get the filename from the URL
    filename=$(basename "$line")

    # Check if the decompressed file already exists
    if [ -f "${filename%.gz}" ]; then
        echo "Decompressed file ${filename%.gz} already exists. Skipping download."
    else
        echo "Downloading file: $line"

        # Download the gzipped file using wget
        wget "$line"

        echo "Decompressing file: $filename"

        # Decompress the file
        gzip -d "$filename"

        echo "Removing downloaded file: $filename"

        # Remove the downloaded gzipped file
        rm "$filename"
    fi

    echo "========================================"

    # Check if the maximum number of iterations is reached
    if [ "$iteration" -eq "$max_iterations" ]; then
        echo "Maximum number of iterations reached. Exiting the loop."
        break
    fi
done < "$file_list"
