from PIL import Image
import numpy as np

# Create a simple 100x100 white image
img = Image.new('RGB', (100, 100), color='white')
img.save('simple_test.jpg', 'JPEG')
print("Simple test image created")
