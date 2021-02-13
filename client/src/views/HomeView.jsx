import React from "react";


import 'bootstrap/dist/css/bootstrap.min.css';

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
            <div className="text-center align-middle">
                <h1>JOIN EVENT</h1>
                <form>
                    <input className="form-control" autoComplete="false" type="text" name="code" placeholder="Enter event code" maxLength="5" pattern="^[0-9a-fA-F]{5}$" onChange={this.handleCodeChange}/>
                    <input className="btn btn-primary m-2" type="submit" value="JOIN" onSubmit={this.submit}/>
                </form>
            </div>
        );
    }

    submit() {
        // Redirect to /event/code
        console.log(this.state.code);
    }
}