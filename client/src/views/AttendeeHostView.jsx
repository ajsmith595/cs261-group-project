import React from "react";
import FeedbackView from "./FeedbackView";
import HostView from "./HostView";

export default class AttendeeHostView extends React.Component {
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
        document.title = "Joining Event " + this.props.match.params.id.toUpperCase();
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + "/api/event/" + this.props.match.params.id, {
            credentials: "include"
        }).then(e => e.json()).then(e => {
            if (e.status != "success") {
                this.setState({
                    status: 'error',
                    message: e.message
                });
            }
            else {
                if (this.props.isHostCallback) {
                    this.props.isHostCallback(e.data.isHost);
                }
                else {
                    console.log(this.props);
                    throw new Error("He2");
                }
                this.setState({
                    status: e.data.isHost ? 'host' : 'attendee',
                    data: e.data
                });
            }
        })
    }
    /**
     * Loads either the attendee or host, depending on the
     * user which is logged in
     * 
     * @returns the corresponding attendee or host view
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
        // Note: this.props.match.params.id gets the :id part of the URL
        else if (this.state.status == 'host') {
            return (
                <HostView eventID={this.props.match.params.id} data={this.state.data} />
            );
        } else {
            return (
                <FeedbackView eventID={this.props.match.params.id} data={this.state.data} />
            );
        }
    }
}