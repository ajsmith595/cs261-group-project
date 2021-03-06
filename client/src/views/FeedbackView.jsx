import React from "react";
import { Form, Col, Row, Button } from "react-bootstrap";
import Multiselect from "react-multi-select-component";
export default class FeedbackView extends React.Component {
    constructor(props) {
        super(props);
        this.sendStateToServer = this.sendStateToServer.bind(this);
        this.state = {
            status: 'show',
            anonymous: false,
            questions: props.data.questions,
            timeError: ""
        };
        if (props.data.error) {
            this.state = {
                status: 'event_error',
                error: props.data.error,
                timeError: ""
            };
        }


    }

    /**
     * Sets the title when mounted
     */
    componentDidMount() {
        document.title = "Feedback on event '" + this.props.data.title + "'";
    }

    /**
     * Sends the feedback data to the server to store and analyse
     */
    sendStateToServer() {
        let responseData = [];
        for (let qid in this.state.questions) {
            let question = this.state.questions[qid];
            let value = question.value;

            if (question.type == "choice" && question.multiple) {
                let newValues = [];
                for (let x of value) {
                    newValues.push(parseInt(x.value));
                }
                value = newValues;
            }
            else if (question.type === "numeric" || question.type == "choice") {
                value = parseInt(value);
            }

            responseData.push({
                questionID: qid,
                response: value
            });
        }
        let dataToSend = {
            anonymous: this.state.anonymous,
            responses: responseData
        };
        this.setState({
            status: 'loading',
            timeError: ""
        });
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + `/api/event/${this.props.eventID}/feedback`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(dataToSend),
                credentials: "include"
            }
        ).then(response => response.json()).then(data => {
            if (data.status === 'success') {
                // Reset all the values of all the questions, so if they want to submit again, the question values are not identical
                for (let i in this.state.questions) {
                    let question = this.state.questions[i];

                    if (question.type === "open") {
                        question.value = "";
                    }
                    else if (question.type === "choice") {
                        if (question.multiple) {
                            question.value = [];
                        }
                        else {
                            question.value = -1;
                        }
                    }
                    else if (question.type === "numeric") {
                        question.value = Math.round((question.min + question.max) / 2);
                    }
                }
                this.setState({
                    status: 'success',
                    questions: this.state.questions,
                    timeError: ""
                });
            }
            else {
                // If error is due to too recent feedback
                // It gives the error and keeps the filled in feedback
                if(data.message == "Feedback was sent recently") {
                    this.setState({
                        status: "show",
                        questions: this.state.questions,
                        anonymous: this.state.anonymous,
                        timeError: "Please wait one minute before sending another piece of feedback"
                    });
                } else {
                    this.setState({
                        status: 'error',
                        timeError: ""
                    })
                }
            }
        }).catch(err => {
            this.setState({
                status: 'error',
                timeError: ""
            })
        });
    }

    /**
     * Renders the feedback-view
     */
    render() {
        if (this.state.status === 'loading') {
            return (
                <h1 className="text-center">Loading...</h1>
            );
        }
        else if (this.state.status === 'success') {
            return (
                <Row>
                    <Col xs={0} sm={1} md={3} />
                    <Col xs={12} sm={10} md={6}>
                        <h1 className="text-center">Feedback Successful!</h1>
                        <Button className="w-100 mx-auto" variant="primary" onClick={() => this.setState({ status: 'show' })}>Give more feedback</Button>
                    </Col>
                    <Col xs={0} sm={1} md={3} />
                </Row>
            );
        }
        else if (this.state.status === 'error') {
            return (
                <Row>
                    <Col xs={0} sm={1} md={3} />
                    <Col xs={12} sm={10} md={6}>
                        <h1 className="text-center">An error occurred!</h1>
                        <Button className="w-100 mx-auto" variant="primary" onClick={() => this.setState({ status: 'show' })}>Try Again?</Button>
                    </Col>
                    <Col xs={0} sm={1} md={3} />
                </Row>
            );
        }
        else if (this.state.status == 'event_error') {
            return (
                <Row>
                    <Col xs={0} sm={1} md={3} />
                    <Col xs={12} sm={10} md={6}>
                        <h1 className="text-center">{this.state.error}</h1>
                    </Col>
                    <Col xs={0} sm={1} md={3} />
                </Row>
            );
        }
        // Gets the rendered question data
        let questions = [];
        for (let id in this.state.questions) {
            let question = this.state.questions[id];
            switch (question.type) {
                case 'open':
                    questions.push(this.renderLongQuestion(question));
                    break;
                case 'numeric':
                    questions.push(this.renderRangeQuestion(question));
                    break;
                case 'choice':
                    if (!question.multiple) {
                        questions.push(this.renderChoiceQuestion(question));
                    } else {
                        questions.push(this.renderMultipleChoiceQuestion(question));
                    }
                    break;
            }
            questions.push(<hr />); //Adds a line after every question
        }
        return (
            <div className="text-center py-2">
                <h1>Feedback on '{this.props.data.title}'</h1>
                <hr />
                {this.renderError()}
                <Form onSubmit={this.sendStateToServer}>
                    {questions}
                    <Form.Check type="checkbox" id="anonymous_check" >
                        <Form.Check.Input type="checkbox" checked={this.state.anonymous} onChange={(e) => this.setState({ anonymous: e.target.checked })} />
                        <Form.Check.Label>Submit the feedback anonymously (the host will not see your username)</Form.Check.Label>
                    </Form.Check>
                    <hr />
                    <Row>
                        <Col xs={0} sm={1} md={3}></Col>
                        <Col xs={12} sm={10} md={6}>
                            <Button className="w-100" type="button" variant="primary" onClick={this.sendStateToServer}>Submit</Button>
                        </Col>
                        <Col xs={0} sm={1} md={3}></Col>
                    </Row>
                </Form>
            </div >
        );
    }

    /*
     * Changes the given questions value based on given ID
     * And which element called the function
     */
    changeQuestion(id) {
        let func = (e) => {
            let questions = this.state.questions;
            questions[id].value = e.target.value; //Gets the value of the caller
            this.setState({
                questions: questions,
            });
        };
        return func.bind(this);
    }

    /*
     * Changes the given questions value based on given ID
     * for the 'multiple-choice question type'
    */
    changeMultiQuestion(id) {
        let func = (e) => {
            let questions = this.state.questions;
            questions[id].value = e; //Requires just e rather than e.target.value
            this.setState({
                questions: questions,
            });
        };
        return func.bind(this);
    }

    /*
     * Renders the long form text question
     * Includes a 'text-area' to enter the response
    */
    renderLongQuestion(question) {
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" onChange={this.changeQuestion(question.id)} value={question.value} placeholder="Enter answer here" as="textarea" rows={4}></Form.Control>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group >
        );
    }

    /*
     * Renders the long form text question
     * Includes a range slider to enter the response
     * and shows the value of the response to the user
     */
    renderRangeQuestion(question) {
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>

                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" value={question.value} onChange={this.changeQuestion(question.id)} type="range" min={question.min} max={question.max} step="1"></Form.Control>
                        <small className="text-center text-muted">{question.value}</small>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group >
        )
    }

    /*
     * Renders the long form text question
     * Gives the user a list of options to choose from
     * The user can only choose at most one response
     */
    renderChoiceQuestion(question) {
        let options = [];
        for (let choice in question.choices) {
            options.push(<option value={choice}>{question.choices[choice]}</option>);
        }
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" as="select" value={question.value} onChange={this.changeQuestion(question.id)}>
                            <option disabled value={-1}>Choose an option...</option>
                            {options}
                        </Form.Control>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group>
        )
    }

    /*
     * Renders the long form text question
     * Gives the user a list of options to choose from
     * The user can choose any amount of answers
     */
    renderMultipleChoiceQuestion(question) {
        let options = [];
        for (let choice in question.choices) {
            options.push({ label: question.choices[choice], value: choice });
        }
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Multiselect className="text-left" options={options} value={this.state.questions[question.id].value} onChange={this.changeMultiQuestion(question.id)} />
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group>
        )
    }

    /**
     * Renders the time related error if it exists
     */
    renderError(){
        if(this.state.timeError === ""){
            return <p></p>;
        }
        else{
            return(
                <div id="Error message">
                <p>{this.state.timeError}</p>
                <hr />
                </div>
            )
        }
    }

}
