use Capstone;

# If adding new posts please do so at the end of the file
#Approved Posts
INSERT INTO post (creationTime, title, descriptionField, postContent, statusId, publishDate) VALUES
(current_timestamp(), "Wives", "Modern day", "Conference appear you wife discussion early common ago. Court no firm interest. Already play though up kitchen. This human enough statement space impact worker prevent.", 1, current_timestamp()),
(current_timestamp(), "Animals", "Animals settle the score", "Mention like trip animal series score system north. Arrive recognize I onto similar decide.", 1, current_timestamp()),
(current_timestamp(), "America's Freedom", "American whole magazine truth stop whose.",  "On traditional measure example sense peace. Would mouth relate own chair. Together range line beyond. First policy daughter need kind miss. Trouble behavior style report size personal partner. During foot that course nothing draw. Language ball floor meet usually board necessary. Natural sport music white. Onto knowledge other his offer face country. Almost wonder employee attorney. Theory type successful together. Raise study modern miss dog Democrat quickly.", 1, current_timestamp()),
(current_timestamp(), "Political Records", "Every manage political record word group food break.", "Picture suddenly drug rule bring determine some forward. Beyond chair recently and.', 'Own available buy country store build before. Already against which continue. Look road article quickly.', 'Per structure attorney author feeling job. Mean always beyond write. Employee toward like total now.', 'Small citizen class morning. Others kind company likely.', 'Themselves true power home price check real. Score from animal exactly drive well good. Pull opportunity throughout take car.", 1, current_timestamp()),
(current_timestamp(), "Security", "Security stock ball organization recognize civil.", "Pm her then nothing increase.', 'Industry product another knowledge else citizen month. Traditional page a although for study anyone. Could yourself plan base rise would.', 'First degree response able state more. Couple part cup few. Beyond take however ball.', 'Son break either president stage population boy. Everything affect American race.', 'Water voice travel among see red. Republican total policy head Mrs debate onto. Catch even front.", 1, current_timestamp()),
(current_timestamp(), "Nature", "Full per among clearly.", "Face house nature fall long dream answer conference. Rock few structure federal board night loss.', 'Buy break marriage also friend reach. Turn phone heart window. Assume be seek article.', 'Hour million large major.', 'Institution happy write end since. Court boy state table agree moment. Budget huge debate among way. Perhaps bit learn gun still.', 'Work chance image quite there many true follow. Your play themselves myself use act relationship.", 1, current_timestamp()),
(current_timestamp(), "Success", "Along chance either six success on.", "At be than always different American address. Former claim chance prevent why measure too.', 'Off question source. Wrong section town deal movement out stay lot. Parent do ten after those scientist.', 'Now four management energy stay significant his. Artist political camera expert stop area.', 'Individual man tell response purpose character would.', 'Partner hit another. Sing after our car food record power. Himself simply make thing particular.", 1, current_timestamp());

# Pending Posts
INSERT INTO post (creationTime, title, descriptionField, postContent, statusId) VALUES
(current_timestamp(), "Employees", "Per structure attorney author feeling job.", "Mean always beyond write. Employee toward like total now.', 'Small citizen class morning. Others kind company likely.', 'Themselves true power home price check real. Score from animal exactly drive well good. Pull opportunity throughout take car.', 'Security stock ball organization recognize civil. Pm her then nothing increase.', 'Industry product another knowledge else citizen month. Traditional page a although for study anyone. Could yourself plan base rise would.', 'First degree response able state more. Couple part cup few. Beyond take however ball.', 'Son break either president stage population boy. Everything affect American race.", 2),
(current_timestamp(), "Water travels", "Water voice travel among see red.", "Republican total policy head Mrs debate onto. Catch even front.', 'Full per among clearly. Face house nature fall long dream answer conference. Rock few structure federal board night loss.', 'Buy break marriage also friend reach. Turn phone heart window. Assume be seek article.', 'Hour million large major.', 'Institution happy write end since. Court boy state table agree moment. Budget huge debate among way. Perhaps bit learn gun still.', 'Work chance image quite there many true follow. Your play themselves myself use act relationship.', 'Along chance either six success on. At be than always different American address. Former claim chance prevent why measure too.", 2),
(current_timestamp(), "Sources Behold", "Off question source.", "Wrong section town deal movement out stay lot. Parent do ten after those scientist.', 'Now four management energy stay significant his. Artist political camera expert stop area.', 'Individual man tell response purpose character would.', 'Partner hit another. Sing after our car food record power. Himself simply make thing particular.', 'Place full buy radio perform small camera treatment. True their race special million. Although hot he couple ground.', 'What top always effort. War project occur. Director simply those physical maybe. Information figure box international not type very.', 'Between similar safe air. Issue indicate market ten foot education good. Grow ahead girl act.", 2),
(current_timestamp(), "Debate vs Argument", "Audience throw debate daughter purpose voice.", "Security fall ready usually.', 'Cost both general where. Agreement decade friend which.', 'Player contain year bill ok choose today. Source firm drug senior.', 'Information animal car after back available. Federal indicate unit opportunity fear great.', 'Plan PM more heavy across while. Kid he weight before control board.', 'Painting child reflect up control instead company. Future model green place beat sense far.', 'Left despite boy without feeling participant interest seem. Question set discussion seven. Place again establish protect a.", 2);

# Rejected Posts
INSERT INTO post (creationTime, title, descriptionField, postContent, statusId) VALUES
(current_timestamp(), "Weather Conditions", "Condition natural can effort bad measure star.", "Law go simple improve language. Need when simple.', 'Drop local cell kid growth main. Why mission because feel.', 'Pull watch choice already thank source she light. In court hospital skin soon. Thought radio minute rich consumer type.', 'Its plant pay together number degree kid drug. Such new cultural analysis care kitchen purpose difference. Technology serious international position. Those full rate clear newspaper.', 'Hospital wrong fish yeah attack detail. Seem west hour less. Decide happy another image because include now.', 'Decision above you carry poor majority herself. At painting room sell daughter pay growth. Seat should dream whose.', 'Tell mention check election order everything stuff detail.", 3),
(current_timestamp(), "Different stuff", "Stuff find different many water meeting future.",  "Fight institution school white.', 'Explain current simply process sit. His can sing husband matter.', 'Prevent hear trouble it grow. Should research executive black tough building. General during cost what.', 'Body himself home message woman. Stock determine human find discussion military ability. First through dinner whose worker offer American.', 'Customer force both something hair. Well account movement can start.', 'Bank letter summer minute perform. At computer doctor up high southern. Decision wish among west agreement girl. Still quality political other begin war.', 'Agency center sign career debate. Cut meet build black treat buy.", 3),
(current_timestamp(), "Networking or Skill", "Break into a new industry", "Nation network college debate direction moment. Ground think save respond friend budget while.', 'Tonight garden maybe forward reason. Worker season figure they yeah get. Memory who yet spend raise child above.', 'Blood single recently other owner message entire. Collection be along break gun reveal five put. Once effect main simply two no. Sister meet though ago.', 'Important produce just raise enough onto try. Those north trouble upon.', 'If do them although per environmental medical nearly. Line Congress must city system story.', 'Attention attention hotel well news enter director. Father growth behind probably.', 'Only cup almost identify. Make real use nice themselves gas best above.", 3);

INSERT INTO rejectionReason (postId, rejectionReason) VALUES
(12, "not enough content"),
(13, "Please change the title"),
(14, "Lorem ipsum");

# If adding new posts please do so after the rejectionReason query
