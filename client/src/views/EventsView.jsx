import React from "react";
import { Col, Row } from "react-bootstrap";
import { Link } from "react-router-dom";
export default class EventsView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            status: 'loading'
        };

    }

    /**
     * When the component is ready to receive calls to this.setState
     * Uses a GET request on /api/event/:id to get data about the event.
     */
    componentDidMount() {
        document.title = "My Events";
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + "/api/events", {
            credentials: "include"
        }).then(e => e.json()).then(e => {
            if (e.status != "success") {
                this.setState({
                    status: 'error',
                    message: e.message
                });
            }
            else {
                let data = e.data.sort((a, b) => b.startTime - a.startTime);
                this.setState({
                    status: 'success',
                    data
                });
            }
        })
    }

    /**
     * Renders the events view page
     */
    render() {
        if (this.state.status == 'loading') {
            return (
                <h1 className="text-center">Loading...</h1>
            );
        }
        else if (this.state.status == 'error') {
            return (
                <h1>{this.state.message}</h1>
            );
        }
        else {
            // Gets the data for each event and presents it to the user
            let events = [];

            for (let event of this.state.data) {
                let startTime = new Date(event.startTime);
                let endTime = new Date(event.startTime + event.duration * 60 * 1000);
                let now = new Date();

                let startTimeOptions = { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
                if (now.getFullYear() != startTime.getFullYear() || now.getFullYear() != endTime.getFullYear() || startTime.getFullYear() != now.getFullYear()) {
                    startTimeOptions['year'] = 'numeric';
                }
                let startTimeString = startTime.toLocaleString([], startTimeOptions);
                let endTimeString = endTime.toLocaleString([], startTimeOptions);
                if (endTime.toDateString() == startTime.toDateString()) {
                    endTimeString = endTime.toLocaleTimeString([], { minute: '2-digit', hour: '2-digit' });
                }


                let active = null;
                if (now > startTime && now < endTime) {
                    active = (
                        <span className="bg-success text-white p-1 border border-dark">ACTIVE</span>
                    )
                }
                events.push(
                    <Col sm={12} md={6} lg={4} className="p-1">
                        <div className="p-4 border rounded event-list-item" onClick={() => this.props.history.push("/event/" + event.eventCode + "?back=events")}>
                            <p className="d-flex align-items-center">
                                <span className="h1 inline-block flex-grow-1">{event.title}</span>{active}
                            </p>
                            <h3>{startTimeString} - {endTimeString}</h3>
                            <h3>Code: {event.eventCode}</h3>
                        </div>
                    </Col>
                )
            }
            if (events.length > 0) {
                return (
                    <Row>
                        {events}
                    </Row>
                )
            }
            else {
                return <div className="text-center pb-2">
                    <h1>No events found</h1>
                    <small>Want to create an event?</small><br />
                    <Link to="/event/new" className="btn btn-primary">Create an Event</Link>
                </div>
            }
        }
    }
}