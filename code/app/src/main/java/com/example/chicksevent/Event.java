package com.example.chicksevent;

import com.google.firebase.database.IgnoreExtraProperties;
@IgnoreExtraProperties
public class Event {
    private String id;

    private String name;
    private String eventDetails;

    private String eventStartDate;
    private String eventEndDate;
    private String registrationStartDate;
    private String registrationEndDate;

    private int entrantLimit;
    private String organizer;
    private String poster;
    private String tag;


    private String waitingList;
    private String lotteryWaitingList;
    private String cancelledEntrants;
    private String finalEntrants;

    public Event() {}


    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEventDetails() { return eventDetails; }
    public void setEventDetails(String eventDetails) { this.eventDetails = eventDetails; }

    public String getEventStartDate() { return eventStartDate; }
    public void setEventStartDate(String eventStartDate) { this.eventStartDate = eventStartDate; }

    public String getEventEndDate() { return eventEndDate; }
    public void setEventEndDate(String eventEndDate) { this.eventEndDate = eventEndDate; }

    public String getRegistrationStartDate() { return registrationStartDate; }
    public void setRegistrationStartDate(String registrationStartDate) { this.registrationStartDate = registrationStartDate; }

    public String getRegistrationEndDate() { return registrationEndDate; }
    public void setRegistrationEndDate(String registrationEndDate) { this.registrationEndDate = registrationEndDate; }

    public int getEntrantLimit() { return entrantLimit; }
    public void setEntrantLimit(int entrantLimit) { this.entrantLimit = entrantLimit; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getWaitingList() { return waitingList; }
    public void setWaitingList(String waitingList) { this.waitingList = waitingList; }

    public String getLotteryWaitingList() { return lotteryWaitingList; }
    public void setLotteryWaitingList(String lotteryWaitingList) { this.lotteryWaitingList = lotteryWaitingList; }

    public String getCancelledEntrants() { return cancelledEntrants; }
    public void setCancelledEntrants(String cancelledEntrants) { this.cancelledEntrants = cancelledEntrants; }

    public String getFinalEntrants() { return finalEntrants; }
    public void setFinalEntrants(String finalEntrants) { this.finalEntrants = finalEntrants; }
}