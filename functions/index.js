const functions = require("firebase-functions");
const admin = require("firebase-admin");
const {Parser} = require("json2csv");

admin.initializeApp();

/**
 * HTTP-triggered function to export the final list of entrants for an event.
 * It reads the list from WaitingList/{eventId}/FINAL and fetches user details.
 * @param {functions.https.Request} req - The HTTP request object.
 * @param {functions.https.Response} res - The HTTP response object.
 */
exports.exportFinalEntrants = functions.https.onRequest(async (req, res) => {
  // Get the eventId from the request URL (e.g., ?eventId=some-event-id)
  const eventId = req.query.eventId;
  if (!eventId) {
    return res.status(400).send("Query parameter 'eventId' is required.");
  }

  try {
    const db = admin.database();
    const finalEntrantsRef = db.ref(`WaitingList/${eventId}/FINAL`);

    const finalEntrantsSnapshot = await finalEntrantsRef.once("value");
    if (!finalEntrantsSnapshot.exists()) {
      // If no final list exists, send a valid but empty CSV file
      res.setHeader("Content-Type", "text/csv");
      const fileName = `final-entrants-${eventId}.csv`;
      // This line was broken up to pass the linting check.
      const disposition = `attachment; filename="${fileName}"`;
      res.setHeader("Content-Disposition", disposition);
      // Send only the headers
      return res.status(200).send("Name,Email\n");
    }

    // Get all user IDs from the FINAL list
    const finalEntrantIds = Object.keys(finalEntrantsSnapshot.val());

    // Fetch all users at once for efficiency
    const usersRef = db.ref("User");
    const allUsersSnapshot = await usersRef.once("value");
    const allUsers = allUsersSnapshot.val();

    const attendeesData = [];

    // For each entrant ID, find their details in the "User" root
    for (const userId of finalEntrantIds) {
      if (allUsers && allUsers[userId]) {
        const userData = allUsers[userId];
        attendeesData.push({
          name: userData.name || "N/A",
          email: userData.email || "N/A",
          // You can add more fields here if they exist
        });
      } else {
        // Handle case where user might be in FINAL list but deleted from User
        attendeesData.push({
          name: "User not found",
          email: userId, // Use ID as an identifier
        });
      }
    }

    // Convert the JSON data to a CSV string using json2csv library
    const fields = ["name", "email"]; // The columns for your CSV
    const json2csvParser = new Parser({fields});
    const csv = json2csvParser.parse(attendeesData);

    // Set HTTP headers to trigger a file download in the browser
    const fileName = `final-entrants-${eventId}.csv`;
    res.setHeader("Content-Type", "text/csv");
    const disposition = `attachment; filename="${fileName}"`;
    res.setHeader("Content-Disposition", disposition);

    // Send the CSV data as the response
    res.status(200).send(csv);
  } catch (error) {
    functions.logger.error("Error exporting final entrants:", error);
    res.status(500).send(
        "An error occurred while generating the CSV file.",
    );
  }
});
