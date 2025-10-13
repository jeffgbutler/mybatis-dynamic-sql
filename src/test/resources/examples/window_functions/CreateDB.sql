--
--    Copyright 2016-2025 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       https://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

create table sales (
    year int not null,
    country varchar(50) not null,
    product varchar(50) not null,
    profit int not null
);

insert into sales values (2000, 'Finland', 'Computer', 1500);
insert into sales values (2000, 'Finland', 'Phone', 100);
insert into sales values (2001, 'Finland', 'Phone', 10);
insert into sales values (2000, 'India', 'Calculator', 75);
insert into sales values (2000, 'India', 'Calculator', 75);
insert into sales values (2000, 'India', 'Computer', 1200);
insert into sales values (2000, 'USA', 'Calculator', 75);
insert into sales values (2000, 'USA', 'Computer', 1500);
insert into sales values (2001, 'USA', 'Calculator', 50);
insert into sales values (2001, 'USA', 'Computer', 1500);
insert into sales values (2001, 'USA', 'Computer', 1200);
insert into sales values (2001, 'USA', 'TV', 150);
insert into sales values (2001, 'USA', 'TV', 100);
