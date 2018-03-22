# QuickReddit
A reddit app for Android to fetch threads from your favourite subreddits!


This app was made as part of the final Capstone project in the Android Developer Nanodegree by Udacity. 

Following is the document made as part of planning the development of the app

• Quick Reddit aims to simplify the Reddit reading experience for a user interested in a number of  subreddits. It prevents the posts from less active subreddits from getting neglected giving equal space to all subreddits


• Quick Reddit is intended for Reddit users who subscribe to a large number of subreddits and find it difficult to view all of them. It fetches posts from the subreddits and displays them so that posts from all subreddits can be seen.

# Features

○ Fetches posts from a saved list of selected subreddits
○ Displays posts from all subreddits at equal intervals
○ Provides hot/new preference for sort order of posts  
○ Displays comments, images and details from threads
○ Handles external links in threads

The MainActivity fetches a list of Reddit posts from the saved subreddits and displays them. Each post item displays the name of the subreddit, the title of the thread and an image associated with the post (if any).
Selecting the overflow menu displays the option to open the SettingsActivity or the activity to display and edit the saved subreddits (SubredditActivity). A layout for the same on a tablet is shown below the mobile device layout.

Detail Activity

The DetailActivity is launched by selecting a Reddit Thread from the MainActivity. 
It displays the image on the MainActivity item in larger size (if any). The subreddit title and the title of the thread are also visible along with the extra text associated with the thread.
A thread may often have an external link which can be viewed using the “Open External Link” button.
The comments and its replies are shown below the information from the post.


Subreddits Activity


This activity displays the saved subreddits to fetch posts from. Clicking on an item removes it from the list. 
The floating action button fetches trending subreddits and displays a list. Clicking on an item now removes or adds the subreddit to the list depending on its existence in the list.
Subreddits can also be searched for using the search option in the menu bar.
Settings Activity


The SettingsActivity displays preferences that can be changed e.g. sort order preference out of hot/new.


App Widget


The widget loads a list of threads using the Reddit API and displays it. The title of the thread & the subreddit and the score of the thread are displayed in the widget. Selecting a thread launches the DetailActivity for the selected thread.
Technical Tasks
Loader to move data to views
The list of subreddits stored in the database is retrieved using a loader and then displayed in a RecyclerView in the SubredditsActivity.

AsyncTask to perform background tasks
As the Volley library handle network requests in a better way than AsyncTask, Volley shall be used for network tasks. AsyncTask shall be used to query the Subreddits database when a search for new subreddits is carried out. The task shall then query the database for each item and check if it already exists in the database, then it shall be marked as saved, if it exists.

# Key Considerations

• How will your app handle data persistence? 

A Content Provider will store the details of the selected subreddits e.g. title and URL of the subreddit to fetch posts from. The posts will then be fetched from these saved subreddits

• Describe any libraries you’ll be using and share your reasoning for including them.

Picasso will be used to fetch and load images in the app from the Internet. ButterKnife will be used to bind views. Timber will be used for ease of logging.Schematic will be used to simplify the process of creating a Content Provider.Volley will be used for handling network requests.

• Describe how you will implement Google Play Services or other external services.

A Reddit reader app having no need for extensive use of Google Play Services, only a few services might be used. Admob may be used to display ads. Firebase Analytics can help in analysing usage of the app.

• Describe any edge or corner cases in the UX

If an image is not available, it shall be replaced with a placeholder image.
On rotation all the views shall be positioned as expected. 
If the devices loses network connectivity, the loaded data shall still be accessible. Any other data views such as comments yet to be loaded shall display an error message.
If there are no subreddits selected to fetch data from, the app shall directly launch the SubredditsActivity to select subreddits to fetch data from.
The sort order(hot/new) for threads can be stored as a preference
An option to clear all the saved subreddits shall be available
The threads in the MainActivity shall be reloadable using a SwipeRefreshLayout
The links in the detail of a thread shall be accessible using an explicit intent
A reddit thread shall be shareable using an explicit intent to share the link of the thread as a text message
The reddit thread and the media file shall be externally accessible in a browser using an intent

• Next Steps: Required Tasks

Task 1: Project Setup. 

• Configure libraries 
• Create a model for a Subreddit class to store title and url of the subreddit
• Fetch list of subreddits using Reddit API
• Parse the JSON data obtained to obtain a list of titles of trending subreddits

Task 2: Implement UI for Activity

• Build UI for  SubredditsActivity to display a list of the titles of subreddits obtained
• Build UI for MainActivity to open SubredditsActivity using overflow menu

Task 3: Complete options for fetching Subreddits


• Create search functionality to fetch subreddits using a query on Reddit API
• Create a ContentProvider and database to store the selected subreddits
• Implement delete functionality for the subreddits from the database


Task 4: Create SettingsActivity

• Create layout for SettingsActivity and Fragment
• Create SharedPreferences Screen to display the sort order preference for posts and any other preference


Task 5: Fetch Posts

• Modify MainActivity to display a list of posts in a RecyclerView
• Create a model for a Thread class to store the details of a thread
• Fetch threads from subreddits (using Reddit API) saved in the list and display in the RecyclerView so that posts from all subreddits appear regularly. 

Task 6: Create DetailActivity

• Build UI for the DetailActivity
• Display the details of the thread 
• Load and display comments for the thread using a query on Reddit API
• Provide support to open external links in the thread


Task 7: Finishing Touches

• Implement Google Play Services
• Handle error cases and fix any crashes
• Create build variants
• Improve the UI to implement Material Design
• Manage resources properly (remove resources that are not required, keep strings in a resource file etc.)
• Clean up code removing unnecessary classes and functions
• Clean project and submit 
