import csv
import json

def json_to_csv(json_file, csv_file, columns_to_keep=None):
    with open(json_file, 'r') as f:
        data = json.load(f)

    if columns_to_keep is not None:
        for row in data:
            # Filtra colonne
            row = {key: value for key, value in row.items() if key in columns_to_keep}

    with open(csv_file, 'w', newline='', encoding='utf-8') as f:
        csv_writer = csv.writer(f)

        # Intestazione
        header = columns_to_keep if columns_to_keep else data[0].keys()
        csv_writer.writerow(header)

        for row in data:
            # Modifica ogni riga
            modified_row = [row[column] for column in columns_to_keep] if columns_to_keep else row.values()
            csv_writer.writerow(modified_row)

# Files
json_file_path = 'boardgames_with_reviews.json'
csv_file_path = 'boardgames_with_reviews.csv'

columns_to_keep = ['boardgameName', 'yearpublished', 'minage', 'reviews']

json_to_csv(json_file_path, csv_file_path, columns_to_keep)

print("Fatto.")
