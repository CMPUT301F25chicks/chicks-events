const functions = require("firebase-functions");
const admin = require("firebase-admin");
const {Parser} = require("json2csv");

admin.initializeApp();

/**
 * HTTP-triggered function to export the ACCEPTED entrants list for an event.
 *
 * Reads entrant IDs from `/WaitingList/{eventId}/ACCEPTED`, fetches user
 * details for each ID from `/User`, converts to CSV, and triggers download.
 *
 * @param {functions.https.Request} request The HTTP request object.
 *   Expects an 'eventId' query parameter.
 * @param {functions.https.Response} response The HTTP response object.
 */
exports.exportFinalEntrants = functions.https.onRequest(async (request,
    response) => {
  try {
    const eventId = request.query.eventId;
    if (!eventId) {
      functions.logger.error("Request is missing eventId query parameter.");
      response.status(400)
          .send("Bad Request: Missing eventId query parameter.");
      return;
    }

    functions.logger.info(`Starting CSV export for eventId: ${eventId}`);

    // --- FIX: Point to the ACCEPTED list ---
    const finalEntrantsRef = admin.database()
        .ref(`/WaitingList/${eventId}/ACCEPTED`);
    const snapshot = await finalEntrantsRef.once("value");

    if (!snapshot.exists()) {
      functions.logger.warn(
          `No entrants in ACCEPTED list for ${eventId}`,
      );
      // Send an empty CSV with headers so the user knows it worked.
      const fields = ["userId", "name", "email", "phone"];
      const json2csvParser = new Parser({fields});
      const csv = json2csvParser.parse([]); // Empty array

      response.setHeader("Content-disposition",
          "attachment; filename=final-entrants.csv");
      response.setHeader("Content-type", "text/csv");
      response.status(200).send(csv);
      return;
    }

    const entrantIds = Object.keys(snapshot.val());
    functions.logger.info(`Found ${entrantIds.length} entrant(s) in ACCEPTED.`);

    const userPromises = entrantIds
        .map((userId) =>
          admin.database().ref(`/User/${userId}`).once("value"),
        );

    const userSnapshots = await Promise.all(userPromises);

    const entrantsData = userSnapshots.map((userSnapshot) => {
      const userId = userSnapshot.key;
      const userData = userSnapshot.val() || {};
      return {
        userId: userId,
        name: userData.name || "N/A",
        email: userData.email || "N/A",
        phone: userData.phone || "N/A",
      };
    });

    const fields = ["userId", "name", "email", "phone"];
    const json2csvParser = new Parser({fields});
    const csv = json2csvParser.parse(entrantsData);

    const fileName = `final-entrants-${eventId}.csv`;
    response.setHeader("Content-disposition",
        `attachment; filename=${fileName}`);
    response.setHeader("Content-type", "text/csv");
    response.status(200).send(csv);

    functions.logger.info(`Successfully sent CSV for eventId: ${eventId}`);
  } catch (error) {
    functions.logger.error("Error generating CSV:", error);
    response.status(500).send("Internal Server Error: Could not make CSV.");
  }
});
