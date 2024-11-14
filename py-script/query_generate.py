import re

def parse_topics_file(input_file, output_file):
    # Regex patterns for extracting fields
    query_id_pattern = re.compile(r"<num> Number: (\d+)")
    title_pattern = re.compile(r"<title>(.*)")
    desc_pattern = re.compile(r"<desc> Description:\s*(.*)")
    narr_pattern = re.compile(r"<narr> Narrative:\s*(.*)")

    # Prepare output list to save parsed queries
    queries = []

    with open(input_file, 'r') as file:
        content = file.read()
        
        # Split the file by each topic section
        topics = content.split("<top>")
        
        for topic in topics:
            # Skip empty sections or malformed entries
            if not topic.strip():
                continue

            # Initialize fields for each topic
            query_id, title, desc, narr = "", "", "", ""
            
            # Extract query ID
            query_id_match = query_id_pattern.search(topic)
            if query_id_match:
                query_id = query_id_match.group(1).strip()
            
            # Extract title
            title_match = title_pattern.search(topic)
            if title_match:
                title = title_match.group(1).strip()
            
            # Extract description
            desc_match = desc_pattern.search(topic)
            if desc_match:
                desc = desc_match.group(1).strip()
                
            # Extract narrative
            narr_match = narr_pattern.search(topic)
            if narr_match:
                narr = narr_match.group(1).strip()

            # Concatenate title, desc, and relevant parts of the narr
            # query_text = f"{title} {desc} {narr}"
            query_text = f"{title} {desc}"
            
            # Clean up any unnecessary line breaks or excessive spaces
            query_text = re.sub(r'\s+', ' ', query_text)
            
            # Add to queries list
            if query_id and query_text:
                queries.append(f"{query_id}\t{query_text}")

    # Write parsed queries to output file
    with open(output_file, 'w') as f:
        for query in queries:
            f.write(query + "\n")

    print(f"Queries file generated at: {output_file}")

# Usage
input_file = './topics'  # Your input file with topics
output_file = 'queries.txt'  # Output file for the generated queries
parse_topics_file(input_file, output_file)
