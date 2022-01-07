
# platform-initiatives
Service for retrieving information about custom defined initiatives across the platform.

This service takes data from various APIs and converts them into an initiative.

## Adding new initiatives
For new initiatives to be displayed on the catalogue, please tag `@PlatOps` on [#int-platform-tech](https://hmrcdigital.slack.com/archives/G0JJ0ADLY).
We will need a description of what you want displayed, along with an API for us to write some Scala implementation 
to make the initiative.

# For developers:

N.B Please make sure that mongodb is running locally. You can run mongo easily in docker by running:

`docker run -d --name --platform-initiatives-mongo mongo`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
