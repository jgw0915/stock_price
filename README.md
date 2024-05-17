# Taiwan Sotck Real Time Information Application
### Introductuin:
This is a small application that use java to show Taiwan real-time stock price. With some functions that you can paly with.

### Requirement:
You need to have mongo DB in yout localhost. The database name and collcetion name instruction will write in the .md file in the database dirctory (currently not update).
You also need to plugin some dependency like Org.JSON

### Stucture and Process:
This application use java.swing to show Information and perform interaction with user. It will call crawler package to get a mapping of stock id and company name at first. Then creating a database instance to fetch exising data in database.The stock information will show in a window as a card shape rectagle.
User can input a stcok id or name in the search bar, The search bar will promt users with matching result of users' input.After user press add buttom, the application will add the stock to the parameter (A list) that used in API and fetch real time data with API. When it get the stock price of the inserted sotck, it will push data to database.
User can also delete stock that he/she doesn't interested with the delete buttom. The application will remove the stock card from window and dekete the sotckdata in database.
If no specific move made. The application will auto refresh the data in every 5 seconds.

### Video Link
https://www.youtube.com/watch?v=Tud89rKznQo
