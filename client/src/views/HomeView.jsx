import React from "react";
import { Redirect } from 'react-router-dom';

import 'bootstrap/dist/css/bootstrap.min.css';
import { Button, Col, Form, Row } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSignInAlt } from "@fortawesome/free-solid-svg-icons";

export default class HomeView extends React.Component {
    constructor(props) {
        super(props);
        this.state = { code: "", completed: false }

        this.buttonClick = this.buttonClick.bind(this);
        this.handleCodeChange = this.handleCodeChange.bind(this);
    }

    componentDidMount() {
        document.title = "Home";
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
        e.preventDefault();
        if (this.state.code.length > 0) {
            this.setState({
                completed: true
            });
            this.props.history.push(`/event/${this.state.code}`);
        }
    }



    render() {
        return (
            <div className="text-center align-middle pt-2">
                <h1>Join Event</h1>
                <hr />
                <Row>
                    <Col lg={4} md={3} sm={0}></Col>
                    <Col lg={4} md={6} sm={12}>
                        <Form className="mx-auto" onSubmit={this.buttonClick}>
                            <Form.Control className="form-control m-2" name="code" placeholder="Enter event code" maxLength="7" onChange={this.handleCodeChange} value={this.state.code} />
                            <Button className="w-100 m-2 font-weight-bold" type="button" variant="primary" onClick={this.buttonClick} disabled={this.state.completed || this.state.code === ""}><FontAwesomeIcon icon={faSignInAlt} /> JOIN</Button>
                        </Form>
                    </Col>
                    <Col lg={4} md={3} sm={0}></Col>
                </Row>
            </div>
        );
    }
}