"""
Contains functionality for creating PyTorch DataLoaders for 
image classification data.
"""

import os
from torchvision import datasets, transforms
from torch.utils.data import DataLoader
import requests
import zipfile
from pathlib import Path

def crete_dataloaders(train_dir: str,
                      test_dir: str,
                      train_transfrom: transforms.Compose,
                      test_transfrom: transforms,
                      batch_size: int):

  NUM_WORKERS = os.cpu_count()

  train_dataset = datasets.ImageFolder(root=train_dir, transform=train_transfrom)
  test_dataset = datasets.ImageFolder(root=test_dir, transform=test_transfrom)

  class_names = train_dataset.classes

  train_dataloader = DataLoader(dataset=train_dataset,
                                batch_size=batch_size,
                                shuffle=True,
                                num_workers=NUM_WORKERS)
  test_dataloader = DataLoader(dataset=test_dataset,
                              batch_size=batch_size,
                              shuffle=False,
                              num_workers=NUM_WORKERS)
  
  return train_dataloader, test_dataloader, class_names

def download_data(source: str, 
                  destination: str,
                  remove_source: bool = True) -> Path:

    # Setup path to data folder
    data_path = Path("data/")
    image_path = data_path / destination

    # If the image folder doesn't exist, download it and prepare it... 
    if image_path.is_dir():
        print(f"[INFO] {image_path} directory exists, skipping download.")
    else:
        print(f"[INFO] Did not find {image_path} directory, creating one...")
        image_path.mkdir(parents=True, exist_ok=True)
        
        # Download pizza, steak, sushi data
        target_file = Path(source).name
        with open(data_path / target_file, "wb") as f:
            request = requests.get(source)
            print(f"[INFO] Downloading {target_file} from {source}...")
            f.write(request.content)

        # Unzip pizza, steak, sushi data
        with zipfile.ZipFile(data_path / target_file, "r") as zip_ref:
            print(f"[INFO] Unzipping {target_file} data...") 
            zip_ref.extractall(image_path)

        # Remove .zip file
        if remove_source:
            os.remove(data_path / target_file)
    
    return image_path