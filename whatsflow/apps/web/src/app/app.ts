import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ChatbotWidgetComponent } from './shared/chatbot/chatbot-widget.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ChatbotWidgetComponent],
  template: `
    <router-outlet />
    <wf-chatbot />
  `,
  styles: [`:host { display: block; min-height: 100vh; }`],
})
export class App {}
