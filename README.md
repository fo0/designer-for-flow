<p align="center"><img src="https://raw.githubusercontent.com/appreciated/designer-for-flow/master/src/main/resources/META-INF/resources/img/logo-floating-low.png">
<br>
<h1>Designer for Flow</h1>
</p> 

This is a WYSIWYG-Editor that allows creating components for Vaadin flow.

Currently the following input and output formats are supported:
- Java Classes    

# Distribution

The Editor is not meant to be run manually. [Instead use the prepackaged Releases](https://github.com/appreciated/designer-for-flow/releases).

# Executing
Before starting the application via IDE execute `mvn clean install`.

For developement purposes the Application can be run using `mvn spring-boot:run` or directly by running the `com.github.appreciated.designer.Application` class from your IDE. 

# Building
To build the project run the following maven command:  
`mvn clean install -Pproduction` 

After executing the ready to use electron application can be found under `target/electron/designer-for-flow-<archtype>`.

# Versioning
The Version of the Editor will be coupled to the respective Vaadin version that was used during the build. The Editor will only use patch versioning. This make it more obvious for the user for which vaadin Version the designer ist designated. 

designer-for-flow v14.0.* -> Vaadin 14.0  
designer-for-flow v14.1.* -> Vaadin 14.1  

# Branching

* `master` the latest version of the starter, using the latest platform snapshot
* `v15` the version for Vaadin 15

# License

This Project is licensed under the GNU General Public License v3.0
