// import { Component, OnInit } from '@angular/core';

// @Component({
//   selector: 'app-create',
//   templateUrl: './create.component.html',
//   styleUrls: ['./create.component.css']
// })
// export class CreateComponent implements OnInit {

//   constructor() { }

//   ngOnInit() {
//   }

// }
import { Component, OnInit } from '@angular/core';
import { FormGroup,  FormBuilder,  Validators } from '@angular/forms';
import { GameService } from '../../game.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css']
})
export class CreateComponent implements OnInit {

 title = 'Add Game';
  angForm: FormGroup;

  constructor(private gameservice: GameService, private fb: FormBuilder,private router: Router,) {
    this.createForm();
   }

   createForm() {
    this.angForm = this.fb.group({
      name: ['', Validators.required ],
      price: ['', Validators.required ]
   });
  }

  addGame(name, price) {
      this.gameservice.addGame(name, price);
  }
  goTo(){
    this.router.navigate(['index']);
  }

  ngOnInit() {
  }

}