import { Component, ElementRef, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from './chatbot.service';

interface ChatMsg {
  role: 'user' | 'bot';
  text: string;
}

@Component({
  selector: 'wf-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot-widget.component.html',
  styleUrl: './chatbot-widget.component.scss',
})
export class ChatbotWidgetComponent {
  open = signal(false);
  draft = '';
  busy = signal(false);
  messages = signal<ChatMsg[]>([
    {
      role: 'bot',
      text: 'Hi — I am the WhatsFlow assistant. Ask about plans, WhatsApp setup, campaigns, or inbox.',
    },
  ]);

  @ViewChild('scroller') scroller?: ElementRef<HTMLDivElement>;

  constructor(private chatbot: ChatbotService) {}

  toggle() {
    this.open.update((v) => !v);
  }

  sendQuick(text: string) {
    this.draft = text;
    this.send();
  }

  send() {
    const text = this.draft.trim();
    if (!text || this.busy()) return;
    this.draft = '';
    this.messages.update((m) => [...m, { role: 'user', text }]);
    this.busy.set(true);
    this.scrollSoon();
    this.chatbot.ask(text).subscribe({
      next: (reply) => {
        this.messages.update((m) => [...m, { role: 'bot', text: reply }]);
        this.busy.set(false);
        this.scrollSoon();
      },
      error: () => {
        this.messages.update((m) => [
          ...m,
          { role: 'bot', text: this.chatbot.localReply(text) },
        ]);
        this.busy.set(false);
        this.scrollSoon();
      },
    });
  }

  private scrollSoon() {
    setTimeout(() => {
      const el = this.scroller?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    }, 40);
  }
}
