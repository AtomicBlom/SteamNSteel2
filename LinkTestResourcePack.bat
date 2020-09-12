mkdir .\run
mkdir .\run\resourcepacks

mklink /j .\test_resource_pack\assets .\src\main\resources\assets
mklink /j .\test_resource_pack\data .\src\main\resources\data
mklink /j .\run\resourcepacks\sns_dev_resource_pack .\test_resource_pack