# meeting-management
Web application to manage meetings using Java Spring Boot.

## About the API

An REST API for meetings management. It is built with Java, Spring Boot, and Spring Framework.
All the meeting data stored in a JSON file (/resources/json/meetings.json). Application retains data between restarts.

## Features

This API provides HTTP endpoints and tools for the following:

* Create new meeting: `POST/meetings`
* Delete the meeting (by the Name and responsible person): `DELETE/meetings/{name}?responsiblePerson={responsiblePerson}`
* Get the list of present meetings: `GET/meetings`
* Get the filtered list of present meetings (e.g. between dates): `GET/meetings?startDate=2023-05-24&endDate=2023-05-27`
* Find the meeting by its name: `GET/meetings/{name}`
* Add participant(s) to the meeting: `PUT/meetings/{name}/addParticipant`
* Remove participant(s) from the meeting: `DELETE/meetings/{name}/removeParticipant`

## Details

### `GET/meetings`

This end-point is called to list all present meetings, that were found in .json file, or were additionally added/removed from the program's meetings' list. 

The filtering option(s) can be assigned to this particular end-point, where each of the filters will be assigned one-by-one. 

e.g. `GET/meetings?description=Java&responsiblePerson=Jonas&startDate=2023-05-24&endDate=2023-05-27`

**Where possible parameters:**

* `description` - filters the list by provided description.
* `responsiblePerson` - filters the list by responsible person.
* `category` - filters the list by meetings' category (Only CodeMonkey, Hub, Short, or TeamBuilding values are accepted).
* `type` - filters the list by meetings' type (Only Live or InPerson values are accepted).
* `startDate` - filters the list by meetings' start date. 
* `endDate` - filters the list by meetings' end date. 
* Note: startDate & endDate parameters assigned together will bring back the meetings between dates provided. 
* `minAttendees` - filters the list by meetings' minimum participants' value. 

### `POST/meetings`

This end-point is called to create a new meeting. If the meeting with the same naming is detected, it will be over-written. 

**Body:**
```json
  {
    "name": "My third meeting",
    "responsiblePerson": "Bill Gates",
    "description": "Meeting to discuss our points and future [PART 3]",
    "meetingCategory": "Hub",
    "meetingType": "InPerson",
    "startDate": "2023-06-20T20:30:00",
    "endDate": "2023-06-20T21:00:00"
  }
```

**Where:**
* `name` - meeting's name.
* `responsiblePerson` - responsible for the meeting person.
* `description` - meeting's description.
* `category` - meeting's category. (Fixed values - CodeMonkey / Hub / Short / TeamBuilding)
* `type` - meeting's type. (Fixed values - Live / InPerson)
* `startDate` - meeting's start date.
* `endDate` - meeting's end date.

Worth to mention, that participants, that are already defined for the meeting, can be provided too:

**Body:**
```json
  {
    "name": "My third meeting",
    "responsiblePerson": "Bill Gates",
    "description": "Meeting to discuss our points and future [PART 3]",
    "meetingCategory": "Hub",
    "meetingType": "InPerson",
    "startDate": "2023-06-20T20:30:00",
    "endDate": "2023-06-20T21:00:00",
    "participants": {
      "Bill Gates": "2023-05-24T15:55:00",
      "Michael Gray": "2023-05-24T15:56:00"
    }
  }
```

**Where:**
`participants` - hash map of the participants of the meeting.

### `DELETE/meetings/{name}?responsiblePerson={responsiblePerson}`

This end-point is called to delete an already existing meeting. Only responsible person has a right to delete the meeting. 

e.g. `DELETE/meetings/My third meeting?responsiblePerson=Bill Gates`

### `PUT/meetings/{name}/addParticipant`

This end-point is called to add a participant(s) to the meeting.

**Body:**
```json
  ["Edvin Michovic", "Aleksandr Guarero", "Michael Holts"]
```

The method of the end-point return the list of the participants. In case of duplicates of participants (those, who were already added), the warning, with the participants' names (who are duplicates), will be shown. In case of no duplicates in the list, it will be shown that participants successfully added. 

### `DELETE/meetings/{name}/removeParticipant`

This end-point is called to remove participant(s) from the meeting.

**Body:**
```json
  ["Edvin Michovic", "Aleksandr Guarero", "Michael Holts"]
```

The participant(s), that are responsible person or/and not present in meeting, will be ignored. The note message is always shown after end-point being called that responsible person *cannot* be deleted from the meeting.
