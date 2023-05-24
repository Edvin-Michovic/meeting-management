# meeting-management
Web application to manage meetings using Java Spring Boot.

## About the API

An REST API for meetings management. It is built with Java, Spring Boot, and Spring Framework.
All the meeting data stored in a JSON file (/resources/json/meetings.json). Application retains data between restarts.

## Features

This API provides HTTP endpoints and tools for the following:

* Create new meeting: `POST/meetings`
* Delete the meeting (by the Name and responsible person): `DELETE/meetings/{name}?responsiblePerson={responsiblePerson}
* Get the list of present meetings: `GET/meetings`
* Get the filtered list of present meetings (e.g. between dates): `GET/meetings?startDate=2023-05-24&endDate=2023-05-27
* Find the meeting by its name: `GET/meetings/{name}`
* Add participant(s) to the meeting: `PUT/meetings/{name}/addParticipant`
* Remove participant(s) from the meeting: `DELETE/meetings/{name}/removeParticipant`

### Details

`GET/meetings`

This end-point is called to list all present meetings, that were found in .json file, or were additionally added/removed from the program's meetings' list. 

The filtering option(s) can be assigned to this particular end-point, where each of the filters will be assigned one-by-one. 

e.g. `GET/meetings?description=Java&responsiblePerson=Jonas&startDate=2023-05-24&endDate=2023-05-27

**Where possible parameters:**

`description` - filters the list by provided description.
`responsiblePerson` - filters the list by responsible person.
`category` - filters the list by meetings' category (Only CodeMonkey, Hub, Short, or TeamBuilding values are accepted).
`type` - filters the list by meetings' type (Only Live or InPerson values are accepted).
`startDate` - filters the list by meetings' start date. 
`endDate` - filters the list by meetings' end date. 
* Note: startDate & endDate parameters assigned together will bring back the meetings between dates provided. 
* minAttendees - filters the list by meetings' minimum participants' value. 
