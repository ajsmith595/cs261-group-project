import React from "react";
import { Form, Col, Row, Button, Card } from "react-bootstrap";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSignInAlt, faExclamationCircle } from '@fortawesome/free-solid-svg-icons';
export default class RegisterView extends React.Component {
    constructor(props) {
        super(props);
        this.sendStateToServer = this.sendStateToServer.bind(this);

        this.state = {
            email: "",
            username: "",
            emailErrorActive: false,
            usernameErrorActive: false,
            serverError: "",
            error: [],
            status: 'main'
        };
    }

    /**
     * Sets the title to Login
     */
    componentDidMount() {
        document.title = "Login";
    }

    /*
     * Sends the state to the server. It first verifies the fields
     * for them being empty and having a valid email
     */
    sendStateToServer(e) {
        e.preventDefault();
        var errors = [];
        if (this.state.acceptTerms == false) {
            errors.push("Please accept the Terms");
        }
        if (this.state.username == "") {
            errors.push("Please enter a username");
        }
        //Tests the email against a regular expression
        /* The expression requires:
            at least 1 character before the @
            An @ symbol
            At least 1 character between the @ and .
            Between 2-15 characters after the .
        */
        if (!(new RegExp(/[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,15}/g).test(this.state.email))) {
            errors.push("Please enter a valid email");
        }
        if (errors != "") {
            this.setState({// Stores the set of errors to be shown to the user
                error: errors,
            });
        } else {
            this.setState({
                status: 'loading'
            })
            fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + '/api/login',
                {
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(this.state),// Sends the state to the server
                    credentials: "include"
                }
            ).then(response => {
                if (response.status >= 400 || !response.ok) {//Throws an error if the server responds witha error status
                    this.setState({
                        status: 'error'
                    });
                }
                response.json().then(data => {
                    if (data.status == "error") {//Custom error sent from server
                        this.setState({
                            error: [], //Empties the errors from validation
                            serverError: data.message,
                            status: 'main'
                        });
                    } else if (data.status == "success") {
                        this.setState({
                            status: 'success'
                        });
                        this.props.history.push("/");
                    }
                })
            }).catch(e => {
                console.log(e);
                this.setState({
                    error: [],
                    serverError: e.getMessage(),
                    status: 'main',
                })
            });
        }
    }

    /* 
     * Renders the Login View
     */
    render() {
        if (this.state.status == 'error') {
            return <div className="text-center">
                <h1>An error occured</h1>
                <Button onClick={() => this.setState({ status: 'main' })}>Retry</Button>
            </div>
        }
        let emailError = !(new RegExp(/[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,15}/g).test(this.state.email));
        let usernameError = this.state.username === "";

        let disabled = this.state.status === 'success' || this.state.status === 'loading' || usernameError || emailError;
        return (
            <div className="py-2">
                <h1 className="text-center">Login</h1>
                <hr />
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <div id="Error message"><p>{this.renderErrors()}</p></div>
                        <Form id="feedback" onSubmit={(e) => { if (!disabled) { this.sendStateToServer(e) } else { this.setState({ emailErrorActive: true, usernameErrorActive: true }) } }}>
                            <Form.Group>
                                <Form.Label for="login_email_input">Email</Form.Label>
                                <Form.Control id="login_email_input" className={`mx-auto ` + ((emailError && this.state.emailErrorActive) ? 'border-danger' : '')} onChange={(e) => this.setState({ email: e.target.value })} value={this.state.email} type="email" placeholder="Enter email" as="input" onBlur={() => this.setState({ emailErrorActive: true })}></Form.Control>
                            </Form.Group>
                            <Form.Group>
                                <Form.Label for="login_username_input">Username</Form.Label>
                                <Form.Control id="login_username_input" className={"mx-auto " + ((usernameError && this.state.usernameErrorActive) ? 'border-danger' : '')} onChange={(e) => this.setState({ username: e.target.value })} value={this.state.username} placeholder="Enter username" as="input" onBlur={() => this.setState({ usernameErrorActive: true })}></Form.Control>
                            </Form.Group>
                            <hr />
                            <Button className="w-100" type="submit" variant="primary" onClick={(e) => this.sendStateToServer(e)} disabled={disabled}><FontAwesomeIcon icon={faSignInAlt} /> Login</Button>
                        </Form>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </div >
        );
    }


    /* 
     * Renders the list of errors, both validation errors and from the server
     */
    renderErrors() {
        let errors = [];
        for (let text in this.state.error) {
            errors.push(<li>{this.state.error[text]}</li>);
        }
        if (this.state.serverError != "") {
            errors.push(<li>{this.state.serverError}</li>);
        }
        if (errors.length == 0) return null;
        return (
            <Card className="" style={{ borderColor: '#f5c6cb' }}>
                <Card.Header style={{ fontVariant: 'small-caps', fontSize: "100%" }} className="alert alert-danger m-0 py-1 px-3"><FontAwesomeIcon icon={faExclamationCircle} /> errors</Card.Header>
                <Card.Body className="py-2 px-4">
                    <ul className="list-unstyled m-0">
                        {errors}
                    </ul>
                </Card.Body>
            </Card>
        )
    }
}