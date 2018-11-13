There are some comments and assumptions which might help to understand some design decisions I made.

- I've made a resource layer (OrderResource) because it is not correct writing REST endpoints in the API.java file.
- The classes have been organized into packages. I know that I shouldn't modify any class, but I needed to give them public visibility because of that reason. The provided clases structure was not modified at all.
- To calculate the distance to de courier, I used the "delivery" location. It was not specified, but it made sense to me.
- I could have made some interfaces to implement from resources/repositories. But since the initial code was using the implementation by default I kept it in that way.
- I didn't make a service layer because the inital code was using the repositories directly from the REST API. I kept it in the same way.