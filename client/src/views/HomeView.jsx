import React from "react";
import { Redirect } from 'react-router-dom';

import 'bootstrap/dist/css/bootstrap.min.css';
import { Button, Col, Form, Row } from "react-bootstrap";

export default class HomeView extends React.Component {
    constructor(props) {
        super(props);
        this.state = { code: "", completed: false }

        this.buttonClick = this.buttonClick.bind(this);
        this.handleCodeChange = this.handleCodeChange.bind(this);
    }

    handleCodeChange(e) {
        let value = e.target.value.toUpperCase();
        let newValue = "";
        for (let c of value) {
            if ("0123456789ABCDEF".includes(c))
                newValue += c;
        }

        this.setState({ code: newValue });
    }

    buttonClick(e) {
        if (this.state.code.length > 0) {
            this.setState({
                completed: true
            });
        }
    }



    render() {
        if (this.state.completed) {
            return <Redirect to={`/event/${this.state.code}`} />
        }
        return (
            <div className="text-center align-middle pt-2">
                <h1>Join Event</h1>
                <hr />
                <Form className="w-25 mx-auto" onSubmit={this.buttonClick}>
                    <Form.Control className="form-control m-2" name="code" placeholder="Enter event code" maxLength="6" onChange={this.handleCodeChange} value={this.state.code} />
                    <Button className="w-100 m-2" type="button" variant="primary" onClick={this.buttonClick}>JOIN</Button>
                </Form>
            </div>
        );
    }
}