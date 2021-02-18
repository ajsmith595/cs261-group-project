import React from "react";


import 'bootstrap/dist/css/bootstrap.min.css';
import {Button, Col, Form, Row} from "react-bootstrap";

export default class HomeView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {code: ""}
    }

    handleCodeChange = (e) => {
        this.setState({code: e.target.value});
        console.log(this.state.code, e.target.value);
    }

    render() {
        return (
            <div className="text-center align-middle pt-2">
                <h1>Join Event</h1>
                <hr />
                <Form method="POST" id="event_join" action="/test/eventJoin">
                    <Row>
                        <Col/>
                        <Col xs={12} sm={10} md={6}>
                            <Form.Control className="form-control m-2" name="code" placeholder="Enter event code" maxLength="5" pattern="^[0-9a-fA-F]{5}$" onChange={this.handleCodeChange}/>
                            <Button className="w-100 m-2" type="button" variant="primary">JOIN</Button>
                        </Col>
                        <Col/>
                    </Row>
                </Form>
            </div>
        );
    }
}