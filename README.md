# A Generic Microservice Template using SpringBoot

#### Overview:
Implementation of a generic API to create/query items and their attachment files. The architecture of the API is designed to support different data models with different attributes. Items and their attachments are stored in MongoDB according to the given collection name.

There are five endpoints of the API:
- `/api/createItem` - Creates a new item in database. There is no restriction about the structure/attributes of an item. The client is expected to provide the item in JSON format and specify the collection name to store it in MongoDB.  
- `/api/queryItems` - Enables querying item objects in database.
- `/api/getItem` - Returns item details for a given item id in database.
- `/api/createAttachment` - Uploads a document (attachment) for an item. Document is inserted as a new file in MongoDB, and the item id for the attachment is stored as metadata of the file.  
- `/api/getAttachment` - Returns the document (attachment) for a given file id in database.


Defining a new type of item does not require any change in this code. It also does not require defining tables or schemas on the server side. 

