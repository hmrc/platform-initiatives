
# platform-initiatives

API for retrieving information about custom defined initiatives across the platform.

## Adding new initiatives
We welcome suggestions from MDTP teams for new platform initiatives.

Any initiatives displayed need to be quantifiable. For example, if there is a dependency upgrade required, we track how 
many need to meet this requirement. Then we track how many are currently meeting this requirement. From this, we can 
deduce how many still need to update.

Before suggesting an initiative you may want to consider:

- Is the proposed initiative measurable?
- What are the dependencies of the proposed initiative?
- Can the initiative you want to track be covered by other tools or approaches? For example, 
  a catalogue dependency explorer search or through analytics
- What is the value of the thing you want to track for the rest of the Platform?

We will need a description of what you want displayed, along with an API for us to write some Scala implementation
to make the initiative.

When contacting PlatOps to suggest an initiative, ideally please provide:
1. A link to an API to get the data from
2. A description of what you want tracked
3. A contact for any further questions we may have

If you have suggestions for new initiatives to be displayed on the catalogue, please tag `@PlatOps` on
[#int-platform-tech](https://hmrcdigital.slack.com/archives/G0JJ0ADLY) and include the information requested above.

# For developers:

N.B Please make sure that mongodb is running locally. You can run mongo easily in docker by running:

`docker run -d --name --platform-initiatives-mongo mongo`

### License

This code is open source software licensed under the 
[Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
