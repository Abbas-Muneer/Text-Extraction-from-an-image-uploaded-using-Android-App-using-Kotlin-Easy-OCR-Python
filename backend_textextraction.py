



import cv2
import easyocr
import matplotlib.pyplot as plt
import fcntl

from flask import Flask, request, jsonify
from flask_cors import CORS  # Import the CORS extension
from werkzeug.utils import secure_filename
import os

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

@app.route('/process_image', methods=['POST'])
def process_image_route():
    if 'file' not in request.files:
        return jsonify({'error': 'No file part'})

    file = request.files['file']

    if file.filename == '':
        return jsonify({'error': 'No selected file'})

    if file:
        filename = secure_filename(file.filename)
        file.save(os.path.join('upload_folder', filename))  # Save the file to a folder
        path = os.path.join('upload_folder', filename)

        # Call your process_image function
        result = process_image(path)

        return jsonify({'result': result})

if __name__ == '__main__':
    app.run(debug=True)



def process_image(image_path):
    img = cv2.imread(image_path)

    reader = easyocr.Reader(['en'], gpu=False)

    text_ = reader.readtext(img)

    listS = []

    threshold = 0.25

    for t in text_:
        bbox, text, score = t

        if score > threshold:
            cv2.rectangle(img, tuple(map(int, bbox[0])), tuple(map(int, bbox[2])), (0, 255, 0), 5)
            cv2.putText(img, text, tuple(map(int, bbox[0])), cv2.FONT_HERSHEY_COMPLEX, 2.5, (255, 0, 0), 2)
            listS.append(text)

    for i in listS:
        print(i)

    # with open('DataOutput.csv', 'a+') as f:
    #     for data in listS:
    #         f.write((str(data) + '\n'))
    #     f.write('\n')

    # plt.imshow(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    # plt.show()


# Example usage
# image_path = '5.jpg'
# process_image(image_path)
