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
    // When the component is ready to receive calls to this.setState
    // Uses a GET request on /api/event/:id to get data about the event.
    componentDidMount() {
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + "/api/event/" + this.props.match.params.id + (process.env.REACT_APP_FORCE_HOST_VIEW ? "?force_host=1" : "")).then(e => e.json()).then(e => {
            if (e.status != "success") {
                this.setState({
                    status: 'error',
                    message: e.message
                });
            }
            else {
                this.setState({
                    status: e.data.isHost ? 'host' : 'attendee',
                    data: e.data
                });
            }
        })
    }
    render() {
        if (this.state.status == 'loading') {
            return (
                <div>
                    <h1>Loading...</h1>
                </div>
            );
        }
        else if (this.state.status == 'error') {
            return (
                <div>
                    <h1>{this.state.message}</h1>
                </div>
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